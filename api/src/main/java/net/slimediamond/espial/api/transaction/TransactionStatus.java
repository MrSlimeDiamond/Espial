package net.slimediamond.espial.api.transaction;

/**
 * The status or callback of a transaction
 *
 * @author SlimeDiamond
 */
public enum TransactionStatus {
  /**
   * This transaction has already been rolled back or restored.
   */
  ALREADY_DONE,
  /**
   * Completed successfully
   */
  SUCCESS,
  /**
   * There is not yet an implementation for this
   */
  UNSUPPORTED,
  /**
   * Something went wrong
   */
  FAILURE
}
