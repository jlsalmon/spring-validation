package com.example.demo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class Pojo {
    @NotNull
    Integer id;
    @NotNull
    @Size(min = 2, max = 2)
    String name;
}