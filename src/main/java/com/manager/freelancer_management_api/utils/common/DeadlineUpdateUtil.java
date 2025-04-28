package com.manager.freelancer_management_api.utils.common;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.exceptions.InvalidDateException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public final class DeadlineUpdateUtil {

    private DeadlineUpdateUtil(){
    }

    public static void applyDeadlineUpdateLogic(Supplier<Deadline> currentDeadlineSupplier, Consumer<Deadline> newDeadlineConsumer, LocalDate newStartDate, LocalDate newEndDate) {

        Deadline currentDeadline = currentDeadlineSupplier.get();
        LocalDate currentStartDate = (currentDeadline != null) ? currentDeadline.getStartDate() : null;
        LocalDate currentEndDate = (currentDeadline != null) ? currentDeadline.getEndDate() : null;

        LocalDate effectiveStartDate = (newStartDate != null) ? newStartDate : currentStartDate;
        LocalDate effectiveEndDate = (newEndDate != null) ? newEndDate : currentEndDate;

        if (effectiveStartDate == null || effectiveEndDate == null) {
            throw new InvalidDateException("Both dates (beginning and end) are required to update the deadline.");
        }
        if (!effectiveEndDate.isAfter(effectiveStartDate)) {
            throw new InvalidDateException("The final date must be after the start date.");
        }

        if (currentDeadline != null &&
                effectiveStartDate.equals(currentDeadline.getStartDate()) &&
                effectiveEndDate.equals(currentDeadline.getEndDate())) {
            return;
        }

        newDeadlineConsumer.accept(new Deadline(effectiveStartDate, effectiveEndDate));
    }
}