package io.muflon.utils.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Problem(
	int status,
	String error,
	String details,
	Set<Violation> violations
) {

	public Problem(HttpStatus status, String error, String details, Set<Violation> violations) {
		this(status.value(), error, details, violations);
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public record Violation(
		String field,
		String message,
		Object rejectedValue
	) {

		static <T> Violation fromConstraintViolation(ConstraintViolation<T> violation) {
			return new Violation(
				violation.getPropertyPath().toString(), violation.getMessage(), violation.getInvalidValue());
		}
	}
}