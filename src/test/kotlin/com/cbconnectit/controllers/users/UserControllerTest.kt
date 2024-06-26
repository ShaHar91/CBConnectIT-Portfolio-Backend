package com.cbconnectit.controllers.users

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.users.UserInstrumentation.givenAValidInsertUser
import com.cbconnectit.controllers.users.UserInstrumentation.givenAValidUpdateUser
import com.cbconnectit.controllers.users.UserInstrumentation.givenAValidUser
import com.cbconnectit.controllers.users.UserInstrumentation.givenAnAlreadyKnownInsertUser
import com.cbconnectit.controllers.users.UserInstrumentation.givenAnInvalidInsertUser
import com.cbconnectit.controllers.users.UserInstrumentation.givenAnInvalidInsertUserWherePasswordIsNotStrong
import com.cbconnectit.controllers.users.UserInstrumentation.givenAnInvalidInsertUserWherePasswordsDontMatch
import com.cbconnectit.controllers.users.UserInstrumentation.givenOldPasswordIsSameAsNewPassword
import com.cbconnectit.controllers.users.UserInstrumentation.givenPasswordNotStrong
import com.cbconnectit.controllers.users.UserInstrumentation.givenPasswordsDontMatch
import com.cbconnectit.controllers.users.UserInstrumentation.givenValidUpdatePassword
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.modules.users.UserController
import com.cbconnectit.modules.users.UserControllerImpl
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidCredentials
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorPasswordsDoNotMatch
import com.cbconnectit.statuspages.ErrorSameAsOldPassword
import com.cbconnectit.statuspages.ErrorUsernameExists
import com.cbconnectit.statuspages.ErrorWeakPassword
import com.cbconnectit.utils.PasswordManagerContract
import com.cbconnectit.utils.toDatabaseString
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest : BaseControllerTest() {

    private val userDao: IUserDao = mockk()
    private val passwordEncryption: PasswordManagerContract = mockk()
    private val controller: UserController by lazy { UserControllerImpl() }

    init {
        startInjection(
            module {
                single { userDao }
                single { passwordEncryption }
            }
        )
    }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(userDao, passwordEncryption)
    }

    @Test
    fun `when creating new user and request object is invalid, we throw an error`() {
        val postUser = givenAnInvalidInsertUser()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postUser(postUser) }
        }
    }

    @Test
    fun `when creating new user and user is already known, we throw an error`() {
        val postUser = givenAnAlreadyKnownInsertUser()

        coEvery { userDao.userUnique(any()) } returns false

        assertThrows<ErrorUsernameExists> {
            runBlocking { controller.postUser(postUser) }
        }
    }

    @Test
    fun `when creating new user and passwords do not match, we throw an error`() {
        val postUser = givenAnInvalidInsertUserWherePasswordsDontMatch()

        coEvery { userDao.userUnique(any()) } returns true

        assertThrows<ErrorPasswordsDoNotMatch> {
            runBlocking { controller.postUser(postUser) }
        }
    }

    @Test
    fun `when creating new user and password is not strong, we throw an error`() {
        val postUser = givenAnInvalidInsertUserWherePasswordIsNotStrong()

        coEvery { userDao.userUnique(any()) } returns true

        assertThrows<ErrorWeakPassword> {
            runBlocking { controller.postUser(postUser) }
        }
    }

    @Test
    fun `when creating new user with valid information, we return user object`() {
        val postUser = givenAValidInsertUser()
        val createdUser = givenAValidUser()

        coEvery { userDao.userUnique(any()) } returns true
        coEvery { passwordEncryption.encryptPassword(any()) } returns ""
        coEvery { userDao.insertUser(any()) } returns createdUser

        runBlocking {
            val responseUser = controller.postUser(postUser)

            assertThat(responseUser.id).isEqualTo(createdUser.id.toString())
            assertThat(responseUser.fullName).isEqualTo(createdUser.fullName)
            assertThat(responseUser.username).isEqualTo(createdUser.username)
            assertThat(responseUser.createdAt).isEqualTo(createdUser.createdAt.toDatabaseString())
            assertThat(responseUser.updatedAt).isEqualTo(createdUser.updatedAt.toDatabaseString())
            assertThat(responseUser.role).isEqualTo(createdUser.role)
        }
    }

    @Test
    fun `when fetching user by id, we return user object`() {
        val user = givenAValidUser()

        coEvery { userDao.getUser(any()) } returns user

        runBlocking {
            val responseUser = controller.getUserById(UUID.randomUUID())

            assertThat(responseUser.id).isEqualTo(user.id.toString())
            assertThat(responseUser.fullName).isEqualTo(user.fullName)
            assertThat(responseUser.username).isEqualTo(user.username)
            assertThat(responseUser.createdAt).isEqualTo(user.createdAt.toDatabaseString())
            assertThat(responseUser.updatedAt).isEqualTo(user.updatedAt.toDatabaseString())
            assertThat(responseUser.role).isEqualTo(user.role)
        }
    }

    @Test
    fun `when fetching user by id, but not existing, we throw error`() {
        coEvery { userDao.getUser(any()) } returns null

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getUserById(UUID.randomUUID()) }
        }
    }

    @Test
    fun `when updating user successfully, we return updated user object`() {
        val updateUser = givenAValidUpdateUser()
        val user = givenAValidUser()

        coEvery { userDao.updateUser(any(), any()) } returns user

        runBlocking {
            val responseUser = controller.updateUserById(UUID.randomUUID(), updateUser)

            assertThat(responseUser.id).isEqualTo(user.id.toString())
            assertThat(responseUser.fullName).isEqualTo(user.fullName)
            assertThat(responseUser.username).isEqualTo(user.username)
            assertThat(responseUser.createdAt).isEqualTo(user.createdAt.toDatabaseString())
            assertThat(responseUser.updatedAt).isEqualTo(user.updatedAt.toDatabaseString())
            assertThat(responseUser.role).isEqualTo(user.role)
        }
    }

    @Test
    fun `when updating user by id, where request object is not valid, we throw error`() {
        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateUserById(UUID.randomUUID(), UpdateUser("", "")) }
        }
    }

    @Test
    fun `when updating user by id, but not existing, we throw error`() {
        val updateUser = givenAValidUpdateUser()

        coEvery { userDao.updateUser(any(), any()) } returns null

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateUserById(UUID.randomUUID(), updateUser) }
        }
    }

    @Test
    fun `when updating password and user does not exist, we throw an error`() {

        coEvery { userDao.getUserHashableById(any()) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.updateUserPasswordById(UUID.randomUUID(), givenValidUpdatePassword()) }
        }
    }

    @Test
    fun `when updating password and old password is same as new, we throw an error`() {
        val postUpdatePassword = givenOldPasswordIsSameAsNewPassword()
        val user = givenAValidUser()

        coEvery { userDao.getUserHashableById(any()) } returns user

        assertThrows<ErrorSameAsOldPassword> {
            runBlocking { controller.updateUserPasswordById(UUID.randomUUID(), postUpdatePassword) }
        }
    }

    @Test
    fun `when updating password and old password is not correct for that user, we throw an error`() {
        val postUpdatePassword = givenPasswordsDontMatch()
        val user = givenAValidUser()

        coEvery { userDao.getUserHashableById(any()) } returns user
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns false

        assertThrows<ErrorInvalidCredentials> {
            runBlocking { controller.updateUserPasswordById(UUID.randomUUID(), postUpdatePassword) }
        }
    }

    @Test
    fun `when updating password and new passwords do not match, we throw an error`() {
        val postUpdatePassword = givenPasswordsDontMatch()
        val user = givenAValidUser()

        coEvery { userDao.getUserHashableById(any()) } returns user
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns true

        assertThrows<ErrorPasswordsDoNotMatch> {
            runBlocking { controller.updateUserPasswordById(UUID.randomUUID(), postUpdatePassword) }
        }
    }

    @Test
    fun `when updating password and password is not strong, we throw an error`() {
        val postUpdatePassword = givenPasswordNotStrong()
        val user = givenAValidUser()

        coEvery { userDao.getUserHashableById(any()) } returns user
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns true

        assertThrows<ErrorWeakPassword> {
            runBlocking { controller.updateUserPasswordById(UUID.randomUUID(), postUpdatePassword) }
        }
    }

    @Test
    fun `when updating password with valid information, we return user object`() {
        val postUpdatePassword = givenValidUpdatePassword()
        val user = givenAValidUser()

        coEvery { userDao.getUserHashableById(any()) } returns user
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns true
        coEvery { passwordEncryption.encryptPassword(any()) } returns "NewPassword"
        coEvery { userDao.updateUserPassword(any(), any()) } returns user

        runBlocking {
            val responseUser = controller.updateUserPasswordById(UUID.randomUUID(), postUpdatePassword)

            assertThat(responseUser.id).isEqualTo(user.id.toString())
            assertThat(responseUser.fullName).isEqualTo(user.fullName)
            assertThat(responseUser.username).isEqualTo(user.username)
            assertThat(responseUser.createdAt).isEqualTo(user.createdAt.toDatabaseString())
            assertThat(responseUser.updatedAt).isEqualTo(user.updatedAt.toDatabaseString())
            assertThat(responseUser.role).isEqualTo(user.role)
        }
    }

    @Test
    fun `when deleting specific user, we return valid userDto`() {

        coEvery { userDao.deleteUser(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteUserById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific user which does not exist, we throw exception`() {

        coEvery { userDao.deleteUser(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteUserById(UUID.randomUUID()) }
        }
    }
}
