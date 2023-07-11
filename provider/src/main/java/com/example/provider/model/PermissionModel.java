package com.example.provider.model;

import lombok.Data;

import java.util.List;

@Data
public class PermissionModel {
    private String scheme;
    private String name;
    private String password;
    private String path;
    private List<String> permissions;
}
