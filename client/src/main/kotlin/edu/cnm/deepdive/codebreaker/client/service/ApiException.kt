package edu.cnm.deepdive.codebreaker.client.service

import edu.cnm.deepdive.codebreaker.client.dto.ErrorResponse

data class ApiException(val response: ErrorResponse) : RuntimeException()