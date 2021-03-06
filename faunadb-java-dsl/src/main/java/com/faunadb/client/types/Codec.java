package com.faunadb.client.types;

import com.faunadb.client.types.Value.*;
import com.faunadb.client.types.time.HighPrecisionTime;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import static java.lang.String.format;

/**
 * Codec is a function that represents an attempt to coerce a {@link Value} to a concrete type.
 * There are pre-defined codecs for each FaunaDB primitive types: {@link Codec#VALUE}, {@link Codec#STRING},
 * {@link Codec#LONG}, {@link Codec#DOUBLE}, {@link Codec#DATE}, {@link Codec#TIME}, {@link Codec#REF},
 * {@link Codec#SET_REF}, {@link Codec#ARRAY}, and {@link Codec#OBJECT}.
 * <p>
 * Codecs return a {@link Result} of the coercion attempt. If it fails to coerce, {@link Result}
 * will contain an error message.
 * <p>
 * It is also possible to create customized codecs to handle complex objects:
 * <pre>{@code
 * class Person {
 *   static final Codec<Person> PERSON = new Codec<Person>() {
 *     public Result<Person> decode(Value value) {
 *       return Result.success(new Person(
 *         value.at("firstName").to(String.class).getOrElse("<no name>"),
 *         value.at("lastName").to(String.class).getOrElse("<no name>")
 *       ));
 *     }
 *
 *     public Result<Value> encode(Person person) {
 *       return Value.from(ImmutableMap.of(
 *         "firstName", person.firstName,
 *         "lastName", person.lastName
 *       ));
 *     }
 *   }
 *
 *   static Person fromValue(Value value) {
 *     return value.to(PERSON);
 *   }
 *
 *   final String firstName, lastName;
 *
 *   Person(String firstName, String lastName) {
 *     this.firstName = firstName;
 *     this.lastName = lastName;
 *   }
 * }
 * }</pre>
 *
 * <p>It is possible to annotate a class and let the internal framework encode/decode instances of the class automatically.</p>
 * <p>Refer to the annotations {@link FaunaField}, {@link FaunaConstructor}, {@link FaunaIgnore} and {@link FaunaEnum} for more details.</p>
 * <p>Also see {@link Encoder}, {@link Decoder} and {@link com.faunadb.client.query.Language#Value(Object)}</p>
 *
 * @param <T> desired resulting type
 * @see Result
 */
public interface Codec<T> {
  Result<T> decode(Value value);
  Result<Value> encode(T value);

  /**
   * Coerce a {@link Value} to itself. Returns a Fail value if an instance of {@link NullV}.
   */
  Codec<Value> VALUE = new Codec<Value>() {
    @Override
    public Result<Value> decode(Value value) {
      if (value == NullV.NULL)
        return Result.fail("Value is null");

      return Result.success(value);
    }

    @Override
    public Result<Value> encode(Value value) {
      if (value == NullV.NULL)
        return Result.fail("Value is null");

      return Result.success(value);
    }
  };

  /**
   * Coerces a {@link Value} to a {@link RefV}
   */
  Codec<RefV> REF = Transformations.mapTo(RefV.class, Functions.<RefV>identity(), Transformations.<RefV, Value>upCast());

  /**
   * Coerces a {@link Value} to a {@link SetRefV}
   */
  Codec<SetRefV> SET_REF = Transformations.mapTo(SetRefV.class, Functions.<SetRefV>identity(), Transformations.<SetRefV, Value>upCast());

  /**
   * Coerces a {@link Value} to a {@link Long}
   */
  Codec<Long> LONG = Transformations.mapTo(LongV.class, Transformations.<LongV, Long>scalarValue(), Transformations.LONG_TO_VALUE);

  /**
   * Coerces a {@link Value} to a {@link Integer}
   */
  Codec<Integer> INTEGER = Transformations.mapWith(LONG, Transformations.LONG_TO_INTEGER, Transformations.INTEGER_TO_LONG);

  /**
   * Coerces a {@link Value} to a {@link Short}
   */
  Codec<Short> SHORT = Transformations.mapWith(LONG, Transformations.LONG_TO_SHORT, Transformations.SHORT_TO_LONG);

  /**
   * Coerces a {@link Value} to a {@link Byte}
   */
  Codec<Byte> BYTE = Transformations.mapWith(LONG, Transformations.LONG_TO_BYTE, Transformations.BYTE_TO_LONG);

  /**
   * Coerces a {@link Value} to a {@link Character}
   */
  Codec<Character> CHAR = Transformations.mapWith(LONG, Transformations.LONG_TO_CHAR, Transformations.CHAR_TO_LONG);

  /**
   * Coerces a {@link Value} to an {@link Instant}
   */
  Codec<Instant> TIME = Transformations.mapTo(TimeV.class, Transformations.VALUE_TO_INSTANT, Transformations.INSTANT_TO_VALUE);

  /**
   * Coerces a {@link Value} to a {@link HighPrecisionTime}
   */
  Codec<HighPrecisionTime> HP_TIME = Transformations.mapTo(TimeV.class, Transformations.<TimeV, HighPrecisionTime>scalarValue(), Transformations.HP_TIME_TO_VALUE);

  /**
   * Coerces a {@link Value} to a {@link String}
   */
  Codec<String> STRING = Transformations.mapTo(StringV.class, Transformations.<StringV, String>scalarValue(), Transformations.STRING_TO_VALUE);

  /**
   * Coerces a {@link Value} to a {@link Double}
   */
  Codec<Double> DOUBLE = Transformations.mapTo(DoubleV.class, Transformations.<DoubleV, Double>scalarValue(), Transformations.DOUBLE_TO_VALUE);

  /**
   * Coerces a {@link Value} to a {@link Float}
   */
  Codec<Float> FLOAT = Transformations.mapWith(DOUBLE, Transformations.DOUBLE_TO_FLOAT, Transformations.FLOAT_TO_DOUBLE);

  /**
   * Coerces a {@link Value} to a {@link Boolean}
   */
  Codec<Boolean> BOOLEAN = Transformations.mapTo(BooleanV.class, Transformations.<BooleanV, Boolean>scalarValue(), Transformations.BOOLEAN_TO_VALUE);

  /**
   * Coerces a {@link Value} to a {@link LocalDate}
   */
  Codec<LocalDate> DATE = Transformations.mapTo(DateV.class, Transformations.<DateV, LocalDate>scalarValue(), Transformations.LOCAL_DATE_TO_VALUE);

  /**
   * Coerces a {@link Value} to an {@link ImmutableList} of {@link Value}
   */
  Codec<ImmutableList<Value>> ARRAY = Transformations.mapTo(ArrayV.class, Transformations.VALUE_TO_LIST, Transformations.LIST_TO_VALUE);

  /**
   * Coerces a {@link Value} to an {@link ImmutableMap} of {@link String} to {@link Value}
   */
  Codec<ImmutableMap<String, Value>> OBJECT = Transformations.mapTo(ObjectV.class, Transformations.VALUE_TO_MAP, Transformations.MAP_TO_VALUE);

  /**
   * Coerces a {@link Value} to an array of bytes
   */
  Codec<byte[]> BYTES = Transformations.mapTo(BytesV.class, Transformations.<BytesV, byte[]>scalarValue(), Transformations.BYTES_TO_VALUE);
}

final class Transformations {

  static <V extends Value, O> Codec<O> mapTo(final Class<V> clazz, final Function<V, O> extractValue, final Function<O, Value> wrapValue) {
    return new Codec<O>() {
      @Override
      public Result<O> decode(Value input) {
        try {
          return cast(clazz, input).map(extractValue);
        } catch (Exception ex) {
          return Result.fail(ex.getMessage(), ex);
        }
      }

      @Override
      public Result<Value> encode(O value) {
        try {
          return Result.success(wrapValue.apply(value));
        } catch (Exception ex) {
          return Result.fail(ex.getMessage(), ex);
        }
      }
    };
  }

  static <I, O> Codec<O> mapWith(final Codec<I> codec, final Function<I, O> mapFunction, final Function<O, I> unmapFunction) {
    return new Codec<O>() {
      @Override
      public Result<O> decode(Value input) {
        try {
          return codec.decode(input).map(mapFunction);
        } catch (Exception ex) {
          return Result.fail(ex.getMessage(), ex);
        }
      }

      @Override
      public Result<Value> encode(O value) {
        try {
          return codec.encode(unmapFunction.apply(value));
        } catch (Exception ex) {
          return Result.fail(ex.getMessage(), ex);
        }
      }
    };
  }

  private static <T> Result<T> cast(Class<T> clazz, Value value) {
    if (clazz.isInstance(value))
      return Result.success(clazz.cast(value));

    return Result.fail(
      format("Can not convert %s to %s", value.getClass().getSimpleName(), clazz.getSimpleName()));
  }

  static <T extends ScalarValue<R>, R> Function<T, R> scalarValue() {
    return new Function<T, R>() {
      @Override
      public R apply(T input) {
        return input.value;
      }
    };
  }

  @SuppressWarnings("unchecked")
  static <I extends O, O> Function<I, O> upCast() {
    return (Function) Functions.identity();
  }

  /// Cast functions

  final static Function<Long, Integer> LONG_TO_INTEGER = new Function<Long, Integer>() {
    @Override
    public Integer apply(Long input) {
      return input.intValue();
    }
  };

  final static Function<Integer, Long> INTEGER_TO_LONG = new Function<Integer, Long>() {
    @Override
    public Long apply(Integer input) {
      return Long.valueOf(input);
    }
  };

  final static Function<Long, Short> LONG_TO_SHORT = new Function<Long, Short>() {
    @Override
    public Short apply(Long input) {
      return input.shortValue();
    }
  };

  final static Function<Short, Long> SHORT_TO_LONG = new Function<Short, Long>() {
    @Override
    public Long apply(Short input) {
      return Long.valueOf(input);
    }
  };

  final static Function<Long, Byte> LONG_TO_BYTE = new Function<Long, Byte>() {
    @Override
    public Byte apply(Long input) {
      return input.byteValue();
    }
  };

  final static Function<Byte, Long> BYTE_TO_LONG = new Function<Byte, Long>() {
    @Override
    public Long apply(Byte input) {
      return Long.valueOf(input);
    }
  };

  final static Function<Long, Character> LONG_TO_CHAR = new Function<Long, Character>() {
    @Override
    public Character apply(Long input) {
      return (char) input.longValue();
    }
  };

  final static Function<Character, Long> CHAR_TO_LONG = new Function<Character, Long>() {
    @Override
    public Long apply(Character input) {
      return Long.valueOf(input);
    }
  };

  final static Function<Double, Float> DOUBLE_TO_FLOAT = new Function<Double, Float>() {
    @Override
    public Float apply(Double input) {
      return input.floatValue();
    }
  };

  final static Function<Float, Double> FLOAT_TO_DOUBLE = new Function<Float, Double>() {
    @Override
    public Double apply(Float input) {
      return Double.valueOf(input);
    }
  };

  /// Wrap functions

  final static Function<Long, Value> LONG_TO_VALUE = new Function<Long, Value>() {
    @Override
    public Value apply(Long input) {
      return new LongV(input);
    }
  };

  final static Function<Double, Value> DOUBLE_TO_VALUE = new Function<Double, Value>() {
    @Override
    public Value apply(Double input) {
      return new DoubleV(input);
    }
  };

  final static Function<String, Value> STRING_TO_VALUE = new Function<String, Value>() {
    @Override
    public Value apply(String input) {
      return new StringV(input);
    }
  };

  final static Function<Boolean, Value> BOOLEAN_TO_VALUE = new Function<Boolean, Value>() {
    @Override
    public Value apply(Boolean input) {
      return BooleanV.valueOf(input);
    }
  };

  final static Function<Instant, Value> INSTANT_TO_VALUE = new Function<Instant, Value>() {
    @Override
    public Value apply(Instant input) {
      return new TimeV(HighPrecisionTime.fromInstant(input));
    }
  };

  final static Function<HighPrecisionTime, Value> HP_TIME_TO_VALUE = new Function<HighPrecisionTime, Value>() {
    @Override
    public Value apply(HighPrecisionTime input) {
      return new TimeV(input);
    }
  };

  final static Function<LocalDate, Value> LOCAL_DATE_TO_VALUE = new Function<LocalDate, Value>() {
    @Override
    public Value apply(LocalDate input) {
      return new DateV(input);
    }
  };

  final static Function<ImmutableMap<String, Value>, Value> MAP_TO_VALUE = new Function<ImmutableMap<String, Value>, Value>() {
    @Override
    public Value apply(ImmutableMap<String, Value> input) {
      return new ObjectV(input);
    }
  };

  final static Function<ImmutableList<Value>, Value> LIST_TO_VALUE = new Function<ImmutableList<Value>, Value>() {
    @Override
    public Value apply(ImmutableList<Value> input) {
      return new ArrayV(input);
    }
  };

  final static Function<byte[], Value> BYTES_TO_VALUE = new Function<byte[], Value>() {
    @Override
    public Value apply(byte[] input) {
      return new BytesV(input);
    }
  };

  /// Unwrap functions

  final static Function<TimeV, Instant> VALUE_TO_INSTANT = new Function<TimeV, Instant>() {
    @Override
    public Instant apply(TimeV time) {
      return time.truncated();
    }
  };

  final static Function<ArrayV, ImmutableList<Value>> VALUE_TO_LIST = new Function<ArrayV, ImmutableList<Value>>() {
    @Override
    public ImmutableList<Value> apply(ArrayV input) {
      return input.values;
    }
  };

  final static Function<ObjectV, ImmutableMap<String, Value>> VALUE_TO_MAP = new Function<ObjectV, ImmutableMap<String, Value>>() {
    @Override
    public ImmutableMap<String, Value> apply(ObjectV input) {
      return input.values;
    }
  };
}