package com.example.service

import com.example.model.User
import com.example.model.UserLevelUpgradeEvent
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.Executors.newFixedThreadPool

@Service
class MailSendingListener(
    private val mailSender: MailSender
) {
    private val executor = newFixedThreadPool(5)

    // 레벨 업그레이드가 실제로 DB에 저장된 후 메일 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserLevelUpgradeEvent(event: UserLevelUpgradeEvent) {
        executor.execute {
            // Handle the user level upgrade event
            sendUpgradeEmail(event.user)
        }
    }

    private fun sendUpgradeEmail(user: User) {
        val message = SimpleMailMessage().apply {
            setTo(user.email)
            from = "admin@example.com"
            subject = "Congratulations! Your account has been upgraded."
            text = "Dear ${user.name},\n\nYour user level has been upgraded to ${user.level}."
        }
        mailSender.send(message)
    }
}