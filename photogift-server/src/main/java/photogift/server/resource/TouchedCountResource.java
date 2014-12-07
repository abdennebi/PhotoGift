package photogift.server.resource;

public interface TouchedCountResource {

    int getCount();

    public class Impl implements TouchedCountResource {

        private final int count;

        public Impl(int count) {
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    public class Assembler {

        public static TouchedCountResource toResource(Long gitfId, int count) {
            return new Impl(count);
        }
    }


}
