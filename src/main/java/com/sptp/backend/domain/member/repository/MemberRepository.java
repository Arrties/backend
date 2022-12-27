package com.sptp.backend.domain.member.repository;

import com.sptp.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUserId(String userId);

    boolean existsByEmail(String email);

    boolean existsByUserId(String userId);
}
