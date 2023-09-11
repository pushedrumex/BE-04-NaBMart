package com.prgrms.nabmart.global.auth.service;

import com.prgrms.nabmart.domain.delivery.Rider;
import com.prgrms.nabmart.global.auth.exception.DuplicateUsernameException;
import com.prgrms.nabmart.domain.delivery.repository.RiderRepository;
import com.prgrms.nabmart.global.auth.service.request.SignupRiderCommand;
import com.prgrms.nabmart.global.auth.service.request.RiderLoginCommand;
import com.prgrms.nabmart.domain.user.UserRole;
import com.prgrms.nabmart.global.auth.exception.InvalidPasswordException;
import com.prgrms.nabmart.global.auth.exception.InvalidUsernameException;
import com.prgrms.nabmart.global.auth.jwt.TokenProvider;
import com.prgrms.nabmart.global.auth.jwt.dto.CreateTokenCommand;
import com.prgrms.nabmart.global.auth.service.response.RiderLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiderAuthenticationService {

    private final RiderRepository riderRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public Long signupRider(SignupRiderCommand signupRiderCommand) {
        checkUsernameDuplication(signupRiderCommand.username());
        Rider rider = createRider(signupRiderCommand);
        riderRepository.save(rider);
        return rider.getRiderId();
    }

    private void checkUsernameDuplication(final String username) {
        if (riderRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException("사용할 수 없는 아이디입니다.");
        }
    }

    private Rider createRider(SignupRiderCommand signupRiderCommand) {
        String encodedPassword = passwordEncoder.encode(signupRiderCommand.password());
        return Rider.builder()
            .username(signupRiderCommand.username())
            .password(encodedPassword)
            .address(signupRiderCommand.address())
            .build();
    }

    @Transactional(readOnly = true)
    public RiderLoginResponse riderLogin(RiderLoginCommand riderLoginCommand) {
        Rider rider = findRiderByUsername(riderLoginCommand);
        checkRiderPassword(riderLoginCommand, rider);
        CreateTokenCommand createTokenCommand
            = CreateTokenCommand.of(rider.getRiderId(), UserRole.ROLE_RIDER);
        String accessToken = tokenProvider.createToken(createTokenCommand);
        return RiderLoginResponse.from(accessToken);
    }

    private Rider findRiderByUsername(RiderLoginCommand riderLoginCommand) {
        return riderRepository.findByUsername(riderLoginCommand.username())
            .orElseThrow(() -> new InvalidUsernameException("사용자의 정보와 일치하지 않습니다."));
    }

    private void checkRiderPassword(RiderLoginCommand riderLoginCommand, Rider rider) {
        if (!passwordEncoder.matches(riderLoginCommand.password(), rider.getPassword())) {
            throw new InvalidPasswordException("사용자의 정보와 일치하지 않습니다.");
        }
    }

}
