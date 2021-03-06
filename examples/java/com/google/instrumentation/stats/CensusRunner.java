/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.stats;

import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import io.grpc.Context;
import java.util.Arrays;

/**
 * Simple program that uses Census contexts.
 */
public class CensusRunner {
  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private static final TagKey K3 = new TagKey("k3");
  private static final TagKey K4 = new TagKey("k4");

  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");
  private static final TagValue V3 = new TagValue("v3");
  private static final TagValue V4 = new TagValue("v4");

  private static final MeasurementUnit simpleMeasurementUnit =
      MeasurementUnit.create(1, Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR }));
  private static final MeasurementDescriptor M1 =
      MeasurementDescriptor.create("m1", "1st test metric", simpleMeasurementUnit);
  private static final MeasurementDescriptor M2 =
      MeasurementDescriptor.create("m2", "2nd test metric", simpleMeasurementUnit);

  public static void main(String[] args) {
    System.out.println("Hello Census World");
    System.out.println("Default Tags: " + DEFAULT);
    System.out.println("Current Tags: " + getCurrentCensusContext());
    Context context1 = withCurrent(DEFAULT.with(K1, V1, K2, V2));
    Context original = context1.attach();
    try {
        System.out.println("  Current Tags: " + getCurrentCensusContext());
        System.out.println("  Current == Default + tags1: "
            + getCurrentCensusContext().equals(getCensusContext(context1)));
        Context context2 = withCurrent(getCurrentCensusContext().with(K3, V3, K4, V4));
        context2.attach();
        try {
          System.out.println("    Current Tags: " + getCurrentCensusContext());
          System.out.println("    Current == Default + tags1 + tags2: "
              + getCurrentCensusContext().equals(getCensusContext(context2)));
          getCurrentCensusContext().record(MeasurementMap.of(M1, 0.2, M2, 0.4));
        } finally {
          context2.detach(context1);
        }
    } finally {
      context1.detach(original);
    }
    System.out.println("Current == Default: "
        + getCurrentCensusContext().equals(DEFAULT));
  }

  private static final CensusContext DEFAULT = Census.getCensusContextFactory().getDefault();

  private static final CensusContext getCurrentCensusContext() {
    return getCensusContext(Context.current());
  }

  private static final CensusContext getCensusContext(Context context) {
    return CensusGrpcContext.getInstance().get(context);
  }

  private static final Context withCurrent(CensusContext context) {
    return CensusGrpcContext.getInstance().withCensusContext(Context.current(), context);
  }
}
