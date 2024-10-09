package io.shinmen.taskmaster.exception;

public class RoleNotFoundException extends TaskMasterException {
    public RoleNotFoundException(String role) {
        super(role + " not found.");
    }

}
