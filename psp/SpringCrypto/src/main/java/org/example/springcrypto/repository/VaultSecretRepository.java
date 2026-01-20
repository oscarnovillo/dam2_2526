package org.example.springcrypto.repository;

import org.example.springcrypto.entity.VaultSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultSecretRepository extends JpaRepository<VaultSecret, Long> {

    List<VaultSecret> findByUserId(Long userId);

    Optional<VaultSecret> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}

