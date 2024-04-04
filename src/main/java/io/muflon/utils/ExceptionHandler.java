package io.muflon.utils;

import io.muflon.utils.problem.Problem;
import io.muflon.utils.problem.ProblemOccurredException;
import io.muflon.utils.problem.Problems;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashSet;
import java.util.Set;

@RestControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
	                                                              HttpStatusCode status, WebRequest request) {
		Set<Problem.Violation> violations = new HashSet<>();

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			violations.add(
				new Problem.Violation(error.getField(), error.getDefaultMessage(), error.getRejectedValue()));
		}
		for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			violations.add(new Problem.Violation(error.getObjectName(), error.getDefaultMessage(), null));
		}

		return ResponseEntity.badRequest().body(Problems.invalidArgumentError(violations));
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return ResponseEntity.badRequest().body(Problems.badRequest(ex.getLocalizedMessage()));
	}

	@org.springframework.web.bind.annotation.ExceptionHandler({ProblemOccurredException.class})
	public ResponseEntity<Object> handleProblemOccurred(ProblemOccurredException ex, WebRequest request) {
		return new ResponseEntity<>(ex.problem(), new HttpHeaders(), ex.problem().status());
	}

	@org.springframework.web.bind.annotation.ExceptionHandler({Exception.class})
	public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
		return ResponseEntity.internalServerError().body(Problems.internalServerError());
	}

}
