package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.ServicesTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.toService
import com.cbconnectit.data.database.tables.toServices
import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.models.service.Service
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class ServiceDaoImpl: IServiceDao {

    override fun getServiceById(id: UUID): Service? =
        fetchServicesWithSubServices(id)

    override fun getServices(): List<Service> =
        fetchServicesRecursive(null)

    private fun fetchServicesWithSubServices(id: UUID): Service? {
        var service: Service? = null

        transaction {
            service = (ServicesTable leftJoin TagsTable).select { ServicesTable.id eq id }
                .toService()

            val subServices = fetchServicesRecursive(id)
            service = service?.copy(subServices = subServices)
        }

        return service
    }

    private fun fetchServicesRecursive(parentId: UUID? = null): List<Service> {
        val subService = mutableListOf<Service>()

        transaction {
            val services = (ServicesTable leftJoin TagsTable).select { ServicesTable.parentServiceId eq parentId }
                .map { it.toService() }

            services.forEach { childService ->
                val grandSubServices = fetchServicesRecursive(childService.id)
                subService.add(childService.copy(subServices = grandSubServices.ifEmpty { null }))
            }
        }

        return subService
    }

    override fun insertService(insertNewService: InsertNewService): Service? {
        val id = ServicesTable.insertAndGetId {
            it[name] = insertNewService.name
            it[parentServiceId] = insertNewService.parentServiceId?.let { id -> UUID.fromString(id) }
            it[tagId] = UUID.fromString(insertNewService.tagId)
        }.value

        return getServiceById(id)
    }

    override fun updateService(id: UUID, updateService: UpdateService): Service? {
        ServicesTable.update({ ServicesTable.id eq id}) {
            it[name] = updateService.name
            it[parentServiceId] = updateService.parentServiceId?.let { parentId -> UUID.fromString(parentId) }
            it[tagId] = UUID.fromString(updateService.tagId)

            it[updatedAt] = LocalDateTime.now()
        }

        return getServiceById(id)
    }

    override fun deleteService(id: UUID): Boolean =
        ServicesTable.deleteWhere { ServicesTable.id eq id } > 0

    override fun getListOfExistingServiceIds(serviceIds: List<UUID>): List<UUID> =
        ServicesTable.select { ServicesTable.id inList serviceIds }.map { it[ServicesTable.id].value }
}