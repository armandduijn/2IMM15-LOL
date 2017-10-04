<?php

namespace App;

use Psr\Container\ContainerInterface;

interface ContainerAwareInterface
{
    /**
     * @param ContainerInterface|null $container
     * @return void
     */
    public function setContainer(ContainerInterface $container = null): void;
}
