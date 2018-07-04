package com.rohitawate.everest.controllers;

/**
 * a searchable item that is used in a search-pane.
 * @author pmucha
 *
 * @param <T>
 */
public interface Searchable<T> {

	int getRelativityIndex(String searchString);

	T getState();

}