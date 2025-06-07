package com.carlosalcina.drivelist.data.datasource

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.utils.Result
import com.carlosalcina.drivelist.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import javax.inject.Inject

class FirestoreCarRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : CarRemoteDataSource {
    
    private val carsForSaleCollection = firestore.collection("coches_venta")
    private val brandsCollection = firestore.collection("marcas")

    override suspend fun fetchBrands(): Result<List<String>, Exception> {
        return try {
            val snapshot = brandsCollection.orderBy("nombre").get().await()
            val brands = snapshot.documents.mapNotNull { it.getString("nombre") }
            Result.Success(brands)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun fetchModels(brandDisplayName: String): Result<List<String>, Exception> {
        return try {
            val brandId = getDocumentIdFromName(brandDisplayName)

            val snapshot = brandsCollection.document(brandId)
                .collection("modelos").orderBy("nombre").get().await()
            val models = snapshot.documents.mapNotNull { it.getString("nombre") }
            Result.Success(models)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun fetchBodyTypes(
        brandDisplayName: String,
        modelDisplayName: String
    ): Result<List<String>, Exception> {
        return try {
            val brandId = getDocumentIdFromName(brandDisplayName)
            val modelId = getDocumentIdFromName(modelDisplayName) // "Serie 3" -> "Serie3"

            val snapshot = brandsCollection.document(brandId)
                .collection("modelos").document(modelId) // USA EL ID SIN ESPACIOS
                .collection("carrocerias").orderBy("tipo").get().await()
            val bodyTypes =
                snapshot.documents.mapNotNull { it.getString("tipo") } // Nombres visibles
            Result.Success(bodyTypes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun fetchFuelTypes(
        brandDisplayName: String, modelDisplayName: String, bodyTypeDisplayName: String
    ): Result<List<String>, Exception> {
        return try {
            val brandId = getDocumentIdFromName(brandDisplayName)
            val modelId = getDocumentIdFromName(modelDisplayName)
            val bodyTypeId = getDocumentIdFromName(bodyTypeDisplayName)

            val snapshot = brandsCollection.document(brandId)
                .collection("modelos").document(modelId)
                .collection("carrocerias").document(bodyTypeId)
                .collection("combustibles").orderBy("tipo").get().await()
            val fuelTypes =
                snapshot.documents.mapNotNull { it.getString("tipo") }
            Result.Success(fuelTypes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun fetchYears(
        brandDisplayName: String,
        modelDisplayName: String,
        bodyTypeDisplayName: String,
        fuelTypeDisplayName: String
    ): Result<List<String>, Exception> {
        return try {
            val brandId = getDocumentIdFromName(brandDisplayName)
            val modelId = getDocumentIdFromName(modelDisplayName)
            val bodyTypeId = getDocumentIdFromName(bodyTypeDisplayName)
            val fuelTypeId = Utils.parseFuel(fuelTypeDisplayName)

            val snapshot = brandsCollection.document(brandId)
                .collection("modelos").document(modelId)
                .collection("carrocerias").document(bodyTypeId)
                .collection("combustibles").document(fuelTypeId)
                .collection("anios")
                .orderBy("anio", Query.Direction.DESCENDING)
                .get()
                .await()

            val years = snapshot.documents.mapNotNull { it.id }

            Result.Success(years)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun fetchVersions(
        brandDisplayName: String,
        modelDisplayName: String,
        bodyTypeDisplayName: String,
        fuelTypeDisplayName: String,
        yearDisplayName: String
    ): Result<List<String>, Exception> {
        try {
            val brandId = getDocumentIdFromName(brandDisplayName)
            val modelId = getDocumentIdFromName(modelDisplayName)
            val bodyTypeId = getDocumentIdFromName(bodyTypeDisplayName)
            val fuelTypeId = Utils.parseFuel(fuelTypeDisplayName)

            val docPath = "marcas/$brandId/modelos/$modelId/carrocerias/$bodyTypeId/combustibles/$fuelTypeId/anios/$yearDisplayName"

            val docRef = firestore.document(docPath)
            val documentSnapshot = docRef.get().await()

            if (!documentSnapshot.exists()) {
                return Result.Success(emptyList())
            }

            val versionsField = documentSnapshot.get("versiones")

            if (versionsField == null) {
                return Result.Success(emptyList())
            }

            if (versionsField is List<*>) {
                val versionsList = mutableListOf<String>()
                for (item in versionsField) {
                    if (item is String) {
                        versionsList.add(item)
                    }
                }
                return Result.Success(versionsList.sorted())
            } else {
                return Result.Error(Exception("El campo 'versiones' tiene un formato inesperado: ${versionsField.javaClass.name}"))
            }

        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun saveCarToFirestore(car: CarForSale): Result<Unit, Exception> {
        return try {
            carsForSaleCollection.document(car.id).set(car).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun editCarFromFirestore(car: CarForSale): Result<Unit, Exception> {
        return try {
            carsForSaleCollection.document(car.id).set(car).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun getDocumentIdFromName(nombreId: String): String {

        var saneado = nombreId

        // 1. Detectar patrones tipo "Coupé 3 puertas" o "Cabrio 2 puertas"
        saneado = saneado.replace(
            Regex("""([A-Za-zñÑáéíóúÁÉÍÓÚüÜ]+?)\s*(\d)\s*puertas""", RegexOption.IGNORE_CASE)
        ) { match ->
            var prefijo = match.groupValues[1]
            val numero = match.groupValues[2]

            // Eliminar última letra solo si es vocal acentuada
            prefijo.lastOrNull()?.let {
                if (it in "áéíóúÁÉÍÓÚ") {
                    prefijo = prefijo.dropLast(1)
                }
            }

            "${prefijo}${numero}puertas"
        }

        // 2. Eliminar acentos
        saneado = Normalizer.normalize(saneado, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

        // 3. Reemplazar '/' por '-'
        saneado = saneado.replace("/", "-")

        // 4. Eliminar caracteres no válidos
        saneado = saneado.replace(Regex("[^a-zA-Z0-9_.-]"), "")

        // 5. Evitar IDs reservados "__...__"
        if (saneado.startsWith("__") && saneado.endsWith("__") && saneado.length > 4) {
            saneado = saneado.removePrefix("__").removeSuffix("__")
        }
        if (Regex("""^__.*__$""").matches(saneado)) {
            saneado = "val_$saneado"
        }


        return saneado
    }

}