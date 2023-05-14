package com.axes.razorcore.cqrs.query;

import java.util.Optional;

public interface ReportQueriesHandler {

    <R extends ReportResult> Optional<R> handleReport(ReportQuery<R> reportQuery);

}
