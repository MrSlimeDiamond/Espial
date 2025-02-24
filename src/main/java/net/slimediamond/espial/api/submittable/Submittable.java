package net.slimediamond.espial.api.submittable;

/**
 * Something which can be submitted and processed.
 *
 * @author Findlay Richardson (SlimeDiamond)
 */
public interface Submittable<T> {
  /** Submit something to be done */
  SubmittableResult<T> submit() throws Exception;
}
