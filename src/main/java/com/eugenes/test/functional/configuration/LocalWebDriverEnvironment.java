package com.eugenes.test.functional.configuration;

class LocalWebDriverEnvironment implements WebDriverEnvironment {

    @Override
    public String getHostname() {
        return "localhost";
    }

}
