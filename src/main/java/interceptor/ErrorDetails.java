package interceptor;

import lombok.Data;

/**
 * ErrorDetails has several parts of information related to additional information of errors.
 *
 * @author Joshua Monson - 11/20/2019
 */
@Data
public class ErrorDetails {

    private int ErrorCode;
    private String ErrorDisplay;
}