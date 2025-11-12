package com.example.demo.strategy.auth;

public abstract class BaseAuthenticationStrategy<T extends Verifiable> implements AuthenticationStrategy {

    private final Class<T> inputDtoType;

    protected abstract AuthenticationResult authenticate(T dto);

    protected BaseAuthenticationStrategy(Class<T> inputDtoType) {
        this.inputDtoType = inputDtoType;
    }

    @Override
    public AuthenticationResult validateAndAuthenticate(Verifiable input) {
        // *** THIS IS THE CRITICAL CHECK ***
        if (!inputDtoType.isInstance(input)) {
            throw new IllegalArgumentException("Invalid input type. Expected "
                    + inputDtoType.getSimpleName() + ", but got "
                    + (input == null ? "null" : input.getClass().getSimpleName()));
        }
        T specificDto = inputDtoType.cast(input);
        return authenticate(specificDto);
    }
}
