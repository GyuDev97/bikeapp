package com.bike.webapp.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MemberAccountsTest {

	private final FakeAuthStore store = new FakeAuthStore();
	private final MemberAccounts accounts = new MemberAccounts(store);

	private static SocialProfile kakao(String id, String nickname) {
		return new SocialProfile("kakao", id, nickname, null);
	}

	@Test
	void firstSignInCreatesTheRiderAndLinksTheAccount() {
		MemberAccounts.Result result = accounts.signIn(kakao("123", "자전거"));
		assertEquals("kakao_123", result.riderId);
		assertTrue(result.created);
		assertTrue(store.riders.contains("kakao_123"));
		assertEquals("kakao_123", store.findLinkedRider("kakao", "123"));
		assertEquals("자전거", store.createdNickname.get("kakao_123"));
	}

	@Test
	void theSecondSignInFindsTheSameRiderAndCreatesNothing() {
		accounts.signIn(kakao("123", "자전거"));
		store.createdNickname.clear();
		MemberAccounts.Result again = accounts.signIn(kakao("123", "다른 이름"));
		assertEquals("kakao_123", again.riderId);
		assertFalse(again.created);
		assertTrue(store.createdNickname.isEmpty());
	}

	@Test
	void anExistingRiderRowIsLinkedNotDuplicated() {
		// a rider the earlier client-only Kakao sign-in may already have records for
		store.riders.add("kakao_123");
		MemberAccounts.Result result = accounts.signIn(kakao("123", "자전거"));
		assertEquals("kakao_123", result.riderId);
		assertFalse(result.created);
		assertEquals("kakao_123", store.findLinkedRider("kakao", "123"));
	}

	@Test
	void naverIdsAreShortenedToFitTheRiderIdColumn() {
		String longId = "AbCdEfGhIjKlMnOpQrStUvWxYz0123456789_-abcdefgh";
		MemberAccounts.Result result = accounts.signIn(new SocialProfile("naver", longId, "산", null));
		assertTrue(result.riderId.startsWith("naver_"));
		assertEquals(6 + 20, result.riderId.length());
		assertEquals(result.riderId, accounts.signIn(new SocialProfile("naver", longId, "산", null)).riderId);
		assertFalse(result.riderId.equals(accounts.signIn(new SocialProfile("naver", longId + "x", "산2", null)).riderId));
	}

	@Test
	void kakaoAndNaverAccountsNeverShareARider() {
		String kakaoRider = accounts.signIn(kakao("7", "a")).riderId;
		String naverRider = accounts.signIn(new SocialProfile("naver", "7", "b", null)).riderId;
		assertFalse(kakaoRider.equals(naverRider));
	}

	@Test
	void aTakenNicknameGetsASuffixAndAMissingOneAFallback() {
		store.nicknames.add("자전거");
		accounts.signIn(kakao("1", "자전거"));
		String nickname = store.createdNickname.get("kakao_1");
		assertTrue(nickname.startsWith("자전거#"), nickname);

		accounts.signIn(kakao("2", null));
		assertEquals("라이더", store.createdNickname.get("kakao_2"));
		accounts.signIn(kakao("3", "   "));
		assertTrue(store.createdNickname.get("kakao_3").startsWith("라이더"));
	}

	@Test
	void aLongNicknameIsCut() {
		accounts.signIn(kakao("1", "가나다라마바사아자차카타파하가나다라마바사아자차카타파하"));
		assertEquals(MemberAccounts.MAX_NICKNAME, store.createdNickname.get("kakao_1").length());
	}
}
