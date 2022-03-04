package com.example.WBMdemo.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.WBMdemo.entity.User;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}