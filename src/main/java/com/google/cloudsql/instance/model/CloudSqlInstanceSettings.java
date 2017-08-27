package com.google.cloudsql.instance.model;

public final class CloudSqlInstanceSettings {
  private Tier tier;
  private PricingPlan pricingPlan;
  private ActivationPolicy activationPolicy;
  private String authorizedNetwork;

  private CloudSqlInstanceSettings(
      Tier tier,
      PricingPlan pricingPlan,
      ActivationPolicy activationPolicy,
      String authorizedNetwork) {
    this.tier = tier;
    this.pricingPlan = pricingPlan;
    this.activationPolicy = activationPolicy;
    this.authorizedNetwork = authorizedNetwork;
  }

  public Tier getTier() {
    return tier;
  }

  public PricingPlan getPricingPlan() {
    return pricingPlan;
  }

  public ActivationPolicy getActivationPolicy() {
    return activationPolicy;
  }

  public String getAuthorizedNetwork() {
    return authorizedNetwork;
  }

  public static CloudSqlInstanceSettings getDefaultSettings() {
    return builder().withDefaults().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Tier tier;
    private PricingPlan pricingPlan;
    private ActivationPolicy activationPolicy;
    private String authorizedNetwork;

    private Builder() {}

    public Builder withDefaults() {
      this.tier = Tier.DB_N1_STANDARD_2;
      this.pricingPlan = PricingPlan.PACKAGE;
      this.activationPolicy = ActivationPolicy.ALWAYS;
      return this;
    }

    public Builder withAuthorizedNetwork(String authorizedNetwork) {
      this.authorizedNetwork = authorizedNetwork;
      return this;
    }

    public CloudSqlInstanceSettings build() {
      return new CloudSqlInstanceSettings(tier, pricingPlan, activationPolicy, authorizedNetwork);
    }
  }

  public enum Tier {
    DB_N1_STANDARD_2("db-n1-standard-2");

    private final String name;

    Tier(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public enum PricingPlan {
    PACKAGE("PACKAGE");

    private final String name;

    PricingPlan(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public enum ActivationPolicy {
    ALWAYS("ALWAYS");

    private final String name;

    ActivationPolicy(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

}
