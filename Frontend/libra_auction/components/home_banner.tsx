import home_banner from '@/public/home_banner.jpg';
import Image from 'next/image';
import HomeBannerContentBox from '@/components/home_banner_content_box';
export default function HomeBanner() {
    return (
        <div className='relative w-full h-180'>
            <div className='absolute size-full'>
                <Image src={home_banner} alt="" className='size-full object-cover'></Image>
            </div>
            <div className='absolute size-full grid grid-cols-2'>
                <div className='col-start-2 px-8 py-18'>
                    <HomeBannerContentBox/>
                </div>
            </div>
        </div>
    );
}