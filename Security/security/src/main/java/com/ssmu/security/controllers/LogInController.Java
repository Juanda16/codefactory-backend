package com.ssmu.security.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ssmu.security.model.AppUser;
import com.ssmu.security.model.AuthenticationReq;
import com.ssmu.security.model.ERole;
import com.ssmu.security.model.Role;
import com.ssmu.security.model.SignupRequest;
import com.ssmu.security.model.response.JwtResponse;
import com.ssmu.security.model.response.MessageResponse;
import com.ssmu.security.repositories.RoleRepository;
import com.ssmu.security.repositories.UserRepository;
import com.ssmu.security.services.JwtUtilService;

import jakarta.validation.Valid;

// import com.ssmu.security.security.jwt.request.JwtRequest;
// import com.ssmu.security.security.jwt.response.JwtResponse;
// import com.ssmu.security.security_config.JwtTokenUtil;

@RestController
@CrossOrigin
@RequestMapping("/api_v1/auth")
public class LoginController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	UserDetailsService usuarioDetailsService;

	@Autowired
	private JwtUtilService jwtUtilService;

	@Autowired
	PasswordEncoder encoder;

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody AuthenticationReq authenticationReq) {
		System.out.println("authenticationReq: " + authenticationReq.toString());

		logger.info("Autenticando al usuario {}", authenticationReq.getuserName());

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(authenticationReq.getuserName(),
						authenticationReq.getpassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	

		final UserDetails userDetails = usuarioDetailsService.loadUserByUsername(
				authenticationReq.getuserName());

		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		System.out.println("userDetails: " + userDetails.toString());

		final String jwt = jwtUtilService.generateToken(userDetails);
		
		System.out.println("jwt: " + jwt);

		return ResponseEntity.ok(new JwtResponse(jwt,
				userDetails.getUsername(),
				roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		AppUser user = new AppUser(
				// signUpRequest.getId(),
				signUpRequest.getUsername(),
				encoder.encode(signUpRequest.getPassword()),
				signUpRequest.getEmail());

		System.out.println("user: " + user.toString());

		Set<String> strRoles = signUpRequest.getRoles();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				System.out.println("role: " + role);
				switch (role) {
					case "admin":
						Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(adminRole);

						break;
					case "mod":
						Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(modRole);

						break;
					default:
						Role userRole = roleRepository.findByName(ERole.ROLE_USER)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@RequestMapping(value = "/currentuser", method = RequestMethod.GET)
	public ResponseEntity<?> getCurrentUser() {
		logger.info("Obteniendo el mensaje");

		var auth = SecurityContextHolder.getContext().getAuthentication();
		logger.info("Datos del Usuario: {}", auth.getPrincipal());
		logger.info("Datos de los Roles {}", auth.getAuthorities());
		logger.info("Esta autenticado {}", auth.isAuthenticated());

		Map<String, String> mensaje = new HashMap<>();
		mensaje.put("Hola Usuario", auth.getPrincipal().toString());
		return ResponseEntity.ok(mensaje);
	}

	@GetMapping("/admin")
	public ResponseEntity<?> getMensajeAdmin() {

		var auth = SecurityContextHolder.getContext().getAuthentication();
		logger.info("Datos del Usuario: {}", auth.getPrincipal());
		logger.info("Datos de los Permisos {}", auth.getAuthorities());
		logger.info("Esta autenticado {}", auth.isAuthenticated());

		Map<String, String> mensaje = new HashMap<>();
		mensaje.put("contenido", "Hola Admin");
		return ResponseEntity.ok(mensaje);
	}
}