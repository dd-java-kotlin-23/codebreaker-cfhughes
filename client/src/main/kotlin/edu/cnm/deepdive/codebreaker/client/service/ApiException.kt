package edu.cnm.deepdive.codebreaker.client.service

import edu.cnm.deepdive.codebreaker.client.dto.ErrorResponse

class ApiException(val response: ErrorResponse): RuntimeException() {

}