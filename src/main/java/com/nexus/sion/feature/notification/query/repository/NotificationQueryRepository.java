package com.nexus.sion.feature.notification.query.repository;

import static com.example.jooq.generated.tables.Notification.NOTIFICATION;
import com.example.jooq.generated.tables.pojos.Notification;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {
    private final DSLContext dsl;

    public List<Notification> selectNotifications(int page, int size, String employeeIdentificationNumber) {
        int offset = (page - 1) * size;

        return dsl.selectFrom(NOTIFICATION)
                .where(NOTIFICATION.RECEIVER_ID.eq(employeeIdentificationNumber))
                .orderBy(NOTIFICATION.CREATED_AT.desc())
                .limit(size)
                .offset(offset)
                .fetchInto(Notification.class);
    }

    public long countTotalNotifications(String employeeIdentificationNumber) {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(NOTIFICATION)
                        .fetchOne(0, Long.class))
                .orElse(0L);
    }
}
