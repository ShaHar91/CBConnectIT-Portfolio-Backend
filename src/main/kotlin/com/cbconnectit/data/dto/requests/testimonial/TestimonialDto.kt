package com.cbconnectit.data.dto.requests.testimonial

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class TestimonialDto(
    val id: String = "",
    @SerializedName("image_url")
    val imageUrl: String = "",
    @SerializedName("full_name")
    val fullName: String = "",
    val company: CompanyDto? = null,
    @SerializedName("job_position")
    val jobPosition: JobPositionDto = JobPositionDto(),
    val review: String = "",
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble
