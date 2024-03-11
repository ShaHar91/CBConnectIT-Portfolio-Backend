package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.*
import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.interfaces.ITestimonialDao
import com.cbconnectit.domain.models.testimonial.Testimonial
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.util.*

class TestimonialDaoImpl : ITestimonialDao {

    override fun getTestimonialById(id: UUID): Testimonial? {

        val testimonialWithRelations = TestimonialsTable leftJoin JobPositionsTable leftJoin CompaniesTable

        val results = testimonialWithRelations.select { TestimonialsTable.id eq id }

        return results.toTestimonial()
    }

    override fun getTestimonials(): List<Testimonial> {
        val testimonialWithRelations = TestimonialsTable leftJoin JobPositionsTable leftJoin CompaniesTable

        val results = testimonialWithRelations.selectAll()

        return results.toTestimonials()
    }

    override fun insertTestimonial(insertNewTestimonial: InsertNewTestimonial): Testimonial? {
        val id = TestimonialsTable.insertAndGetId {
            it[imageUrl] = insertNewTestimonial.imageUrl
            it[fullName] = insertNewTestimonial.fullName
            it[review] = insertNewTestimonial.review
            it[jobPositionId] = UUID.fromString(insertNewTestimonial.jobPositionId)
            it[companyId] = UUID.fromString(insertNewTestimonial.companyId)
        }.value

        return getTestimonialById(id)
    }

    override fun updateTestimonial(id: UUID, updateTestimonial: UpdateTestimonial): Testimonial? {
        TestimonialsTable.update({ TestimonialsTable.id eq id }) {
            it[imageUrl] = updateTestimonial.imageUrl
            it[fullName] = updateTestimonial.fullName
            it[review] = updateTestimonial.review
            it[jobPositionId] = UUID.fromString(updateTestimonial.jobPositionId)
            it[companyId] = UUID.fromString(updateTestimonial.companyId)

            it[updatedAt] = LocalDateTime.now()
        }

        return getTestimonialById(id)
    }

    override fun deleteTestimonial(id: UUID): Boolean =
        TestimonialsTable.deleteWhere { TestimonialsTable.id eq id } > 0
}