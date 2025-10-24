package service

import com.example.model.User
import com.example.model.UserLevel
import com.example.model.UserLevelUpgradeEvent
import com.example.service.MailSendingListener
import io.kotest.core.spec.style.FunSpec
import io.mockk.*
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import java.util.concurrent.TimeUnit

class MailSendingListenerTest : FunSpec({

    val mailSender: MailSender = mockk(relaxed = true)
    val listener = MailSendingListener(mailSender)

    beforeTest {
        clearAllMocks()
    }

    test("레벨 업그레이드 이벤트 발생 시 메일 전송") {
        // given
        val user = User("user1", "홍길동", "password", UserLevel.BASIC, 50, 10, "user1@example.com")
        val event = UserLevelUpgradeEvent(user)

        // when
        listener.handleUserLevelUpgradeEvent(event)

        // then
        verify(timeout = 1000) {
            val message = withArg<SimpleMailMessage> {
                it.subject == "레벨 업그레이드 안내"
                it.text!!.contains("홍길동님, 축하합니다! 레벨이 업그레이드 되었습니다.")
                it.to.contentEquals(arrayOf("user1@example.com"))
            }
            mailSender.send(message)
        }
    }

    test("메일 발송 실패 시 예외가 전파되지 않음") {
        // given
        val user = User("user1", "홍길동", "password", UserLevel.BASIC, 50, 10, "invalid@email")
        val event = UserLevelUpgradeEvent(user)

        every { mailSender.send(any<SimpleMailMessage>()) } throws RuntimeException("메일 발송 실패")

        // when & then (예외 발생하지 않아야 함)
        listener.handleUserLevelUpgradeEvent(event)

        // 비동기 처리 대기
        TimeUnit.SECONDS.sleep(1)

        verify { mailSender.send(any<SimpleMailMessage>()) }
    }

    test("여러 사용자의 레벨 업그레이드 이벤트 처리") {
        // given
        val users = listOf(
            User("user1", "홍길동", "pw", UserLevel.BASIC, 50, 10, "user1@example.com"),
            User("user2", "김철수", "pw", UserLevel.SILVER, 60, 30, "user2@example.com")
        )

        // when
        users.forEach { user ->
            listener.handleUserLevelUpgradeEvent(UserLevelUpgradeEvent(user))
        }

        // 비동기 처리 대기
        TimeUnit.SECONDS.sleep(2)

        // then
        verify(exactly = 2) {
            mailSender.send(any<SimpleMailMessage>())
        }
    }
})
