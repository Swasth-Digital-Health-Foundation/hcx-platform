declare module 'swiper' {
  import { SwiperOptions } from 'swiper/types';

  export const Navigation: any;
  export const Pagination: any;
  export const Autoplay: any; 
  export const Swiper: any;
  export const SwiperSlide: any;

  export interface SwiperInstance {
    navigation: {
      init: () => void;
      update: () => void;
    };
  }

  export default function Swiper(
    container: HTMLElement | string,
    options?: SwiperOptions
  ): SwiperInstance;
}
