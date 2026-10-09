package ru.burdak.game.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.burdak.game.user.dto.UserStateRequestDto;
import ru.burdak.game.user.service.UserService;

import java.net.http.HttpRequest;
import java.util.UUID;

@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private UserService userService;

  @PutMapping("/{userId}/state")
  public void setOrUpdateUsersState(HttpServletRequest request,
                                    @PathVariable(name = "userId", required = false)UUID userId,
                                    @RequestBody @Valid UserStateRequestDto dto) {
 log.info("Получен запрос {} {}", request.getMethod(), request.getRequestURI());
    userService.setOrUpdateUsersState(userId, dto);
  }



}
