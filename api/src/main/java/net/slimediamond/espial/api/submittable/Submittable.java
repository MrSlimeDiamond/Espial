package net.slimediamond.espial.api.submittable;

/**
 * Something which can be submitted and processed.
 *
 * @author SlimeDiamond
 */
public interface Submittable<T> {
  /** Submit something to be done */
  SubmittableResult<T> submit() throws Exception;
}
