package dev.streamx.cli.licence;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LicenceContext {

  private boolean acceptLicence;

  public boolean isAcceptLicence() {
    return acceptLicence;
  }

  public void setAcceptLicence(boolean acceptLicence) {
    this.acceptLicence = acceptLicence;
  }
}
