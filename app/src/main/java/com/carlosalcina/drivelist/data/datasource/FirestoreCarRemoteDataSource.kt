package com.carlosalcina.drivelist.data.datasource

import com.carlosalcina.drivelist.data.remote.MeiliSearchApi
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.utils.Result
import com.carlosalcina.drivelist.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

class FirestoreCarRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    @Named("MeiliSearch") private val meiliSearchApi: MeiliSearchApi
) : CarRemoteDataSource {

    private val carsForSaleCollectionRef = firestore.collection("coches_venta")

    override suspend fun fetchBrands(): Result<List<String>, Exception> {
        return try {
            val snapshot = firestore.collection("marcas").orderBy("nombre").get().await()
            val brands = snapshot.documents.mapNotNull { it.getString("nombre") }
            Result.Success(brands)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun fetchModels(brandDisplayName: String): Result<List<String>, Exception> {
        return try {
            val brandId = brandDisplayName

            val snapshot = firestore.collection("marcas").document(brandId)
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
            val brandId = brandDisplayName
            val modelId = getDocumentIdFromName(modelDisplayName) // "Serie 3" -> "Serie3"

            val snapshot = firestore.collection("marcas").document(brandId)
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
            val brandId = brandDisplayName
            val modelId = getDocumentIdFromName(modelDisplayName)
            val bodyTypeId =
                getDocumentIdFromName(bodyTypeDisplayName) // "Berlina 4 puertas" -> "Berlina4puertas"

            val snapshot = firestore.collection("marcas").document(brandId)
                .collection("modelos").document(modelId)
                .collection("carrocerias").document(bodyTypeId) // USA EL ID
                .collection("combustibles").orderBy("tipo").get().await()
            val fuelTypes =
                snapshot.documents.mapNotNull { it.getString("tipo") } // Nombres visibles
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
            val brandId = brandDisplayName
            val modelId = getDocumentIdFromName(modelDisplayName)
            val bodyTypeId = getDocumentIdFromName(bodyTypeDisplayName)
            val fuelTypeId = Utils.parseFuel(getDocumentIdFromName(fuelTypeDisplayName))

            val snapshot = firestore.collection("marcas").document(brandId)
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
            val brandId = brandDisplayName
            val modelId = getDocumentIdFromName(modelDisplayName)
            val bodyTypeId = getDocumentIdFromName(bodyTypeDisplayName)
            val fuelTypeId = Utils.parseFuel(getDocumentIdFromName(fuelTypeDisplayName))

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
            meiliSearchApi.addOrUpdateCars(listOf(car))
            carsForSaleCollectionRef.document(car.id).set(car).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun getDocumentIdFromName(name: String): String {
        val generatedId = name.replace(" ", "")
        return generatedId
    }
}