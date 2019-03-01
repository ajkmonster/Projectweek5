package com.example.demo;

import org.springframework.data.repository.CrudRepository;

public interface MessageRespository extends CrudRepository<Message, Long> {

}
