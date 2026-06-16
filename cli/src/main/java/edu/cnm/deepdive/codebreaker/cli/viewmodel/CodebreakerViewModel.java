package edu.cnm.deepdive.codebreaker.cli.viewmodel;

import edu.cnm.deepdive.codebreaker.service.CodebreakerService;

public class CodebreakerViewModel {

  private final CodebreakerService service;

  public CodebreakerViewModel() {
    service = CodebreakerService.getInstance();
  }

}
