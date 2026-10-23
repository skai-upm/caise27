package es.upm.etsisi.park.presentation;

import java.io.PrintStream;

import es.upm.etsisi.park.business.exception.ParkException;

/** Executes one command with its already split and trimmed arguments. */
@FunctionalInterface
public interface CommandHandler {

    void execute(String[] args, PrintStream out) throws ParkException;
}
