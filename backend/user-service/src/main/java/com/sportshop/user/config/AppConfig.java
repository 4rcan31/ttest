package com.sportshop.user.config;

import java.time.Clock;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;

import com.sportshop.user.notification.LoggingPasswordResetNotifier;
import com.sportshop.user.notification.PasswordResetNotifier;
import com.sportshop.user.notification.SmtpPasswordResetNotifier;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordResetNotifier passwordResetNotifier(UserServiceProperties properties,
                                                ObjectProvider<JavaMailSender> mailSender) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (properties.mail().enabled() && sender != null) {
            return new SmtpPasswordResetNotifier(sender, properties.mail().from());
        }
        return new LoggingPasswordResetNotifier();
    }
}
