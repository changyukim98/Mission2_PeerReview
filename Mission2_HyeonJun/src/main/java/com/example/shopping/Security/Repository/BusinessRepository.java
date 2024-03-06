package com.example.shopping.Security.Repository;

import com.example.shopping.Security.Entity.BusinessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<BusinessEntity, Long> {
}
