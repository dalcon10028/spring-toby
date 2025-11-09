package service.user

import com.example.service.user.UserService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.aop.framework.ProxyFactoryBean

//class UserServiceTransactionTest : FunSpec({
//    val txUserService: UserService = mockk()
//
//    beforeEach {
//        // Setup code before each test if needed
//        val userService = UserService(
//            userDao = mockk(),
//            publisher = mockk()
//        )
//
//        ProxyFactoryBean().apply {
//            setTarget(userService)
//            isProxyTargetClass = true // Use CGLIB proxying
//            addAdvisor(
//                DefaultPointcutAdvisor(
//                    NameMatchMethodPointcut().apply {
//                        setMappedName("upgradeLevels")
//                    },
//                    TransactionInterceptor()
//                )
//            )
//            txUserService = getObject() as UserService
//        }
//    }
//
//
//})
