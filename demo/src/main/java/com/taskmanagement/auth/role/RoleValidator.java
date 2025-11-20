package com.taskmanagement.auth.role;


import com.taskmanagement.user.enums.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class RoleValidator implements ConstraintValidator<ValidRole, Role> {

    private Role[] allowedRoles;

    @Override
    public void initialize(ValidRole constraintAnnotation) {
        this.allowedRoles = constraintAnnotation.allowedRoles();
    }

    @Override
    public boolean isValid(Role role, ConstraintValidatorContext context) {
        if (role == null) {
            return true;
        }

        return Arrays.asList(allowedRoles).contains(role);
    }
}