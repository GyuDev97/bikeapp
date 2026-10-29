package com.bike.webapp.auth;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Finds the rider of a verified social account, creating the rider the first time. */
@Service
public class MemberAccounts {

	static final int MAX_NICKNAME = 20;
	private static final String DEFAULT_NICKNAME = "라이더";

	public static class Result {
		public final String riderId;
		public final boolean created;

		Result(String riderId, boolean created) {
			this.riderId = riderId;
			this.created = created;
		}
	}

	private final AuthStore store;
	private final SecureRandom random = new SecureRandom();

	@Autowired
	public MemberAccounts(AuthStore store) {
		this.store = store;
	}

	public Result signIn(SocialProfile profile) {
		String linked = store.findLinkedRider(profile.getProvider(), profile.getUserId());
		if (linked != null) {
			store.touchLogin(profile.getProvider(), profile.getUserId());
			return new Result(linked, false);
		}

		String riderId = riderIdFor(profile);
		boolean created = false;
		if (!store.riderExists(riderId)) {
			store.createRider(riderId, unusablePassword(), freeNickname(profile));
			created = true;
		}
		if (!store.link(profile.getProvider(), profile.getUserId(), riderId)) {
			// a parallel sign-in linked it first
			String winner = store.findLinkedRider(profile.getProvider(), profile.getUserId());
			return new Result(winner == null ? riderId : winner, false);
		}
		return new Result(riderId, created);
	}

	/**
	 * Kakao ids are short numbers and keep the form the app already used ("kakao_123"). Other ids may
	 * be long, so they are shortened with a hash to fit the existing rider id column.
	 */
	static String riderIdFor(SocialProfile profile) {
		if ("kakao".equals(profile.getProvider())) {
			return "kakao_" + profile.getUserId();
		}
		String hash = AuthTokenService.sha256(profile.getProvider() + ":" + profile.getUserId());
		return profile.getProvider() + "_" + hash.substring(0, 20);
	}

	private String freeNickname(SocialProfile profile) {
		String base = profile.getNickname() == null ? DEFAULT_NICKNAME : profile.getNickname().trim();
		if (base.length() > MAX_NICKNAME) {
			base = base.substring(0, MAX_NICKNAME);
		}
		if (base.isEmpty()) {
			base = DEFAULT_NICKNAME;
		}
		String candidate = base;
		for (int attempt = 0; attempt < 5 && store.nicknameTaken(candidate); attempt++) {
			candidate = base + "#" + (1000 + random.nextInt(9000));
		}
		return candidate;
	}

	/** Not a password anyone can type: the former password login no longer exists. */
	private String unusablePassword() {
		byte[] bytes = new byte[24];
		random.nextBytes(bytes);
		return "!social!" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
