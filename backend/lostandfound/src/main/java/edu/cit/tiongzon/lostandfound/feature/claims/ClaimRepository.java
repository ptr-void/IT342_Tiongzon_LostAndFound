package edu.cit.tiongzon.lostandfound.feature.claims;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByClaimant_UserId(Long userId);
    List<Claim> findByItem_Reporter_UserId(Long userId);
    List<Claim> findByItem_Id(Long itemId);
}
