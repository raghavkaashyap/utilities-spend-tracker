type ApiError = {
  response?: {
    data?: {
      error?: string;
      message?: string;
    };
  };
  message?: string;
};

export function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error) {
    return error.message;
  }

  if (typeof error === "object" && error !== null) {
    const apiError = error as ApiError;
    return apiError.response?.data?.error ?? apiError.response?.data?.message ?? apiError.message ?? fallback;
  }

  return fallback;
}
