package com.techinventory.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techinventory.catalogo.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
