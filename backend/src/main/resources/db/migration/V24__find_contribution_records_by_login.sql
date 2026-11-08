-- Public profiles are addressed by GitHub login, which GitHub compares without case.
CREATE INDEX contribution_records_login ON contribution_records (lower(login));
