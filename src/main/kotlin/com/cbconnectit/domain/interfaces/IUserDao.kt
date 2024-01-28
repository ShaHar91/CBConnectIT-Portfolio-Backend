package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.domain.models.user.User

interface IUserDao {

    fun getUser(id: Int): User?
    fun getUserHashableById(id: Int): User?
    fun getUserHashableByUsername(username: String): User?
    fun getUsers(): List<User>
    fun insertUser(user: InsertNewUser): User?

    fun updateUser(id: Int, user: UpdateUser): User?
    fun deleteUser(id: Int): Boolean
    fun userUnique(username: String): Boolean
    fun isUserRoleAdmin(userId: Int): Boolean
    fun updateUserPassword(userId: Int, updatePassword: String): User?
}