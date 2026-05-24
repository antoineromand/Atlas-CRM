package com.antoineromand.atlascrm.api.shared;

import java.util.Map;

public record ApiErrorResponse(String code, String message, int status, Map<String, Object> details) {}
