package Projet.Microservice.Exceptions.UsersExceptions;

public class UserBannedException extends RuntimeException {
    public UserBannedException(String message) {
        super(message);
    }
}
